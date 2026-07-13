import React, { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { ProgressBar } from '../components/ProgressBar';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ChevronRight, ChevronDown, Plus } from 'lucide-react';

interface Goal {
  id: string;
  name: string;
  parentId: string | null;
  targetValue: number;
  currentValue: number;
  status: string;
  children?: Goal[];
}

const buildTree = (goals: Goal[], parentId: string | null = null): Goal[] => {
  return goals
    .filter(g => g.parentId === parentId)
    .map(g => ({ ...g, children: buildTree(goals, g.id) }));
};

const GoalNode: React.FC<{ goal: Goal; level: number }> = ({ goal, level }) => {
  const [expanded, setExpanded] = useState(true);
  const hasChildren = goal.children && goal.children.length > 0;
  const pct = goal.targetValue > 0 ? (goal.currentValue / goal.targetValue) * 100 : 0;

  return (
    <div className="w-full">
      <div 
        className="flex items-center justify-between p-4 border-b border-gray-700/50 hover:bg-gray-800/50 transition"
        style={{ paddingLeft: `${level * 2 + 1}rem` }}
      >
        <div className="flex items-center gap-3 flex-1">
          <button 
            onClick={() => setExpanded(!expanded)}
            className="w-6 h-6 flex items-center justify-center text-gray-400 hover:text-white"
          >
            {hasChildren ? (expanded ? <ChevronDown size={18} /> : <ChevronRight size={18} />) : <span className="w-4" />}
          </button>
          <div>
            <div className="font-medium text-gray-200">{goal.name}</div>
            <div className="text-xs text-gray-500 mt-1">Target: {goal.targetValue} | Current: {goal.currentValue}</div>
          </div>
        </div>
        
        <div className="w-48 hidden md:block">
          <ProgressBar progress={pct} />
        </div>
        
        <div className="w-24 text-right">
          <span className="text-xs bg-gray-700 text-gray-300 px-2 py-1 rounded">{goal.status}</span>
        </div>
        
        <div className="w-12 text-right">
          <button className="text-gray-400 hover:text-primary-400" title="Add Child Goal">
            <Plus size={18} />
          </button>
        </div>
      </div>
      
      {expanded && hasChildren && (
        <div className="w-full">
          {goal.children!.map(child => (
            <GoalNode key={child.id} goal={child} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  );
};

export default function GoalTreePage() {
  const [showModal, setShowModal] = useState(false);
  const [level, setLevel] = useState('COMPANY');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [parentId, setParentId] = useState('');

  const { data: rawGoals, isLoading, refetch } = useQuery({
    queryKey: ['goals'],
    queryFn: async () => (await apiClient.get('/v1/goals?page=0&size=100')).data.content as Goal[]
  });

  const saveMutation = useMutation({
    mutationFn: async (data: any) => await apiClient.post('/v1/goals', data),
    onSuccess: () => {
      setShowModal(false);
      refetch();
    }
  });

  const tree = rawGoals ? buildTree(rawGoals, null) : [];

  const handleSave = () => {
    saveMutation.mutate({
      title,
      description,
      level,
      ...(level === 'DEPARTMENT' ? { parentGoalId: parentId } : {})
    });
  };

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader 
        title="Goal Hierarchy" 
        action={
          <button 
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
            onClick={() => {
              setLevel('COMPANY');
              setTitle('');
              setDescription('');
              setParentId('');
              setShowModal(true);
            }}
          >
            <Plus size={16} /> Create Goal
          </button>
        }
      />

      {isLoading ? <LoadingSpinner /> : (
        <div className="bg-gray-900 border border-gray-700 rounded-xl overflow-hidden shadow">
          {tree.length > 0 ? tree.map(goal => (
            <GoalNode key={goal.id} goal={goal} level={0} />
          )) : (
            <div className="p-8 text-center text-gray-400">No goals found.</div>
          )}
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">Create Goal</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Level</label>
                <select name="level" value={level} onChange={e => setLevel(e.target.value)} className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white">
                  <option value="COMPANY">Company</option>
                  <option value="DEPARTMENT">Department</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">Title</label>
                <input name="title" value={title} onChange={e => setTitle(e.target.value)} className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white" />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">Description</label>
                <textarea name="description" value={description} onChange={e => setDescription(e.target.value)} className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white" />
              </div>
              {level === 'DEPARTMENT' && (
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Parent Goal</label>
                  <select name="parentGoalId" value={parentId} onChange={e => setParentId(e.target.value)} className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white">
                    <option value="">Select Parent</option>
                    {rawGoals?.filter(g => !g.parentId).map(g => (
                      <option key={g.id} value={g.id}>{g.name}</option>
                    ))}
                  </select>
                </div>
              )}
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button onClick={() => setShowModal(false)} className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg">Cancel</button>
              <button onClick={handleSave} className="px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg">Save Goal</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
